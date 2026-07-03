import { useState } from 'react'
import './App.css'

const API_URL = 'https://payment-reconciliation-service-y91x.onrender.com'

function App() {
  const [token, setToken] = useState(null)
  const [loginForm, setLoginForm] = useState({ username: '', password: '' })
  const [loginError, setLoginError] = useState('')

  const [internalFile, setInternalFile] = useState(null)
  const [bankFile, setBankFile] = useState(null)
  const [summary, setSummary] = useState(null)
  const [matched, setMatched] = useState([])
  const [unmatched, setUnmatched] = useState([])
  const [discrepancies, setDiscrepancies] = useState([])
  const [message, setMessage] = useState('')

  const handleLogin = async () => {
    setLoginError('')
    try {
      const res = await fetch(`${API_URL}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(loginForm)
      })
      const data = await res.json()
      if (data.token) {
        setToken(data.token)
      } else {
        setLoginError(data.error || 'Login failed')
      }
    } catch (e) {
      setLoginError('Login failed: ' + e.message)
    }
  }

  const handleLogout = () => {
    setToken(null)
    setSummary(null)
    setLoginForm({ username: '', password: '' })
  }

  const authHeaders = () => ({ 'Authorization': `Bearer ${token}` })

  const uploadFile = (file, source) => {
    const formData = new FormData()
    formData.append('file', file)
    return fetch(`${API_URL}/api/transactions/import?source=${source}`, {
      method: 'POST',
      headers: authHeaders(),
      body: formData
    }).then(res => res.text())
  }

  const handleUpload = async () => {
    if (!internalFile || !bankFile) {
      setMessage('Please select both files first')
      return
    }
    setMessage('Uploading...')
    try {
      await fetch(`${API_URL}/api/transactions/clear`, {
        method: 'DELETE',
        headers: authHeaders()
      })
      await uploadFile(internalFile, 'INTERNAL')
      await uploadFile(bankFile, 'BANK')
      setMessage('Both files uploaded successfully')
    } catch (e) {
      setMessage('Upload failed: ' + e.message)
    }
  }

  const handleReconcile = async () => {
    setMessage('Running reconciliation...')
    try {
      const res = await fetch(`${API_URL}/api/reconciliation/run`, {
        method: 'POST',
        headers: authHeaders()
      })
      const summaryData = await res.json()
      setSummary(summaryData)

      const m = await fetch(`${API_URL}/api/transactions/matched`, { headers: authHeaders() }).then(r => r.json())
      const u = await fetch(`${API_URL}/api/transactions/unmatched`, { headers: authHeaders() }).then(r => r.json())
      const d = await fetch(`${API_URL}/api/transactions/discrepancies`, { headers: authHeaders() }).then(r => r.json())
      setMatched(m)
      setUnmatched(u)
      setDiscrepancies(d)
      setMessage('Reconciliation complete')
    } catch (e) {
      setMessage('Reconciliation failed: ' + e.message)
    }
  }

  const TransactionTable = ({ title, data }) => (
      <div style={{ marginTop: '1.5rem' }}>
        <h3>{title} ({data.length})</h3>
        {data.length === 0 ? (
            <p style={{ color: '#888' }}>None</p>
        ) : (
            <table border="1" cellPadding="8" style={{ borderCollapse: 'collapse' }}>
              <thead>
              <tr>
                <th>Source</th><th>Reference</th><th>Amount</th><th>Date</th><th>Description</th>
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

  if (!token) {
    return (
        <div style={{ padding: '2rem', fontFamily: 'sans-serif', maxWidth: '400px' }}>
          <h1>Login</h1>
          <div style={{ marginBottom: '0.5rem' }}>
            <input
                placeholder="Username"
                value={loginForm.username}
                onChange={e => setLoginForm({ ...loginForm, username: e.target.value })}
            />
          </div>
          <div style={{ marginBottom: '0.5rem' }}>
            <input
                type="password"
                placeholder="Password"
                value={loginForm.password}
                onChange={e => setLoginForm({ ...loginForm, password: e.target.value })}
            />
          </div>
          <button onClick={handleLogin}>Log In</button>
          {loginError && <p style={{ color: 'red' }}>{loginError}</p>}
        </div>
    )
  }

  return (
      <div style={{ padding: '2rem', fontFamily: 'sans-serif', maxWidth: '900px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h1>Payment Reconciliation Service</h1>
          <button onClick={handleLogout}>Log Out</button>
        </div>

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