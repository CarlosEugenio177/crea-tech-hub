import { useEffect, useState } from 'react'

const TOKEN_PATTERN = /^[A-Za-z0-9_-]{43}$/

function readTokenFromFragment() {
  const token = new URLSearchParams(window.location.hash.slice(1)).get('token')
  return token && TOKEN_PATTERN.test(token) ? token : undefined
}

export function useResetToken() {
  const [token] = useState(readTokenFromFragment)

  // O token sai da barra de endereço para não ficar no histórico do navegador.
  useEffect(() => {
    if (!window.location.hash) return
    const { pathname, search } = window.location
    window.history.replaceState(window.history.state, '', pathname + search)
  }, [])

  return token
}
