import { useQueryClient } from '@tanstack/react-query'
import { api } from '../../shared/api/client'

const SESSION_PATH = '/api/v1/admin/auth/me'
const FORM_HEADERS = { 'Content-Type': 'application/x-www-form-urlencoded' }

type Credentials = { email: string; password: string }

function useSessionCache() {
  const queryClient = useQueryClient()
  const { queryKey } = api.queryOptions('get', SESSION_PATH)

  return {
    refresh: () => queryClient.invalidateQueries({ queryKey }),
    clear: () => void queryClient.resetQueries({ queryKey }),
    // Remove sem buscar de novo: quem chamou navega para o login em seguida.
    forget: () => queryClient.removeQueries({ queryKey }),
  }
}

export function useAdminSession() {
  return api.useQuery('get', SESSION_PATH, {}, { retry: false })
}

export function useLogin() {
  const session = useSessionCache()
  const login = api.useMutation('post', '/api/v1/admin/auth/login', {
    onSuccess: session.refresh,
  })

  return {
    error: login.error,
    isPending: login.isPending,
    submit: (credentials: Credentials) =>
      login.mutate({ body: credentials, headers: FORM_HEADERS }),
  }
}

export function useLogout() {
  const session = useSessionCache()
  return api.useMutation('post', '/api/v1/admin/auth/logout', {
    onSettled: session.clear,
  })
}

export function useChangePassword(onChanged: () => void) {
  const session = useSessionCache()
  return api.useMutation('post', '/api/v1/admin/auth/password', {
    onSuccess: () => {
      session.forget()
      onChanged()
    },
  })
}

export function useRequestPasswordRecovery() {
  return api.useMutation('post', '/api/v1/admin/auth/password/forgot')
}

export function useResetPassword() {
  return api.useMutation('post', '/api/v1/admin/auth/password/reset')
}
