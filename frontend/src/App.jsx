import { useState } from 'react'
import './App.css'

function App() {
  const [internalFile, setInternalFile] = useState(null)
  const [bankFile, setBankFile] = useState(null)
  const [summary, setSummary] = useState(null)
  const [matched, setMatched] = useState([])
  const [unmatched, setUnmatched] = useState([])
  const [discrepancies, setDiscrepancies] = useState([])
  const [message, setMessage] = useState('')

  // Upload one CSV file to the backend
  const uploadFile = (file, source) => {
    const formData = new FormData()
    formData.append('file', file)

    return fetch(`http://localhost:8080/api/transactions/import?source=${source}`, {
      method: 'POST',
      body: formData
    }).then(res => res.text())
  }

  // Handle uploading both files
  const handleUpload = async () => {
    if (!internalFile || !bankFile) {
      setMessage('Please select both files first')
      return
    }
    setMessage('Uploading...')
    try {
      // Clear old data first so re-uploads don't create duplicates
      await fetch('http://localhost:8080/api/transactions/clear', { method: 'DELETE' })

      await uploadFile(internalFile, 'INTERNAL')
      await uploadFile(bankFile, 'BANK')
      setMessage('Both files uploaded successfully')
    } catch (e) {
      setMessage('Upload failed: ' + e.message)
    }
  }

  // Run reconciliation and load all results
  const handleReconcile = async () => {
    setMessage('Running reconciliation...')
    try {
      const res = await fetch('http://localhost:8080/api/reconciliation/run', { method: 'POST' })
      const summaryData = await res.json()
      setSummary(summaryData)

      // Load the detailed results
      const m = await fetch('http://localhost:8080/api/transactions/matched').then(r => r.json())
      const u = await fetch('http://localhost:8080/api/transactions/unmatched').then(r => r.json())
      const d = await fetch('http://localhost:8080/api/transactions/discrepancies').then(r => r.json())
      setMatched(m)
      setUnmatched(u)
      setDiscrepancies(d)
      setMessage('Reconciliation complete')
    } catch (e) {
      setMessage('Reconciliation failed: ' + e.message)
    }
  }

  // A reusable table for showing transactions
  const TransactionTable = ({ title, data }) => (
      <div style={{ marginTop: '1.5rem' }}>
        <h3>{title} ({data.length})</h3>
        {data.length === 0 ? (
            <p style={{ color: '#888' }}>None</p>
        ) : (
            <table border="1" cellPadding="8" style={{ borderCollapse: 'collapse' }}>
              <thead>
              <tr>
                <th>Source</th>
                <th>Reference</th>
                <th>Amount</th>
                <th>Date</th>
                <th>Description</th>
              </tr>
              </thead>
              <tbody>
              {data.map(t => (
                  <tr key={t.id}>
                    <td>{t.source}</td>
                    <td>{t.reference}</td>
                    <td>{t.amount}</td>
                    <td>{t.transactionDate}</td>
                    <td>{t.description}</td>
                  </tr>
              ))}
              </tbody>
            </table>
        )}
      </div>
  )

  return (
      <div style={{ padding: '2rem', fontFamily: 'sans-serif', maxWidth: '900px' }}>
        <h1>Payment Reconciliation Service</h1>

        <div style={{ marginBottom: '1rem' }}>
          <h3>1. Upload Files</h3>
          <div style={{ marginBottom: '0.5rem' }}>
            <label>Internal records: </label>
            <input type="file" accept=".csv" onChange={e => setInternalFile(e.target.files[0])} />
          </div>
          <div style={{ marginBottom: '0.5rem' }}>
            <label>Bank statement: </label>
            <input type="file" accept=".csv" onChange={e => setBankFile(e.target.files[0])} />
          </div>
          <button onClick={handleUpload}>Upload Both Files</button>
        </div>

        <div style={{ marginBottom: '1rem' }}>
          <h3>2. Run Reconciliation</h3>
          <button onClick={handleReconcile}>Run Reconciliation</button>
        </div>

        {message && <p style={{ fontWeight: 'bold' }}>{message}</p>}

        {summary && (
            <div style={{ marginTop: '1rem', padding: '1rem', border: '2px solid #333', display: 'inline-block' }}>
              <h3>Summary</h3>
              <p>Matched: {summary.matched}</p>
              <p>Discrepancies: {summary.discrepancies}</p>
              <p>Unmatched: {summary.unmatched}</p>
            </div>
        )}

        {summary && (
            <>
              <TransactionTable title="Matched" data={matched} />
              <TransactionTable title="Discrepancies" data={discrepancies} />
              <TransactionTable title="Unmatched" data={unmatched} />
            </>
        )}
      </div>
  )
}

export default App