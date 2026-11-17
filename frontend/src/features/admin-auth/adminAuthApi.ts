import { useQueryClient } from '@tanstack/react-query'
import { useEffect, useState } from 'react'
import { problemStatus } from '../../shared/api/problem.ts'
import { adminSessionEvents, api } from '../../shared/api/client.ts'
import type { components } from '../../shared/api/schema'

type LoginRequest = components['schemas']['LoginRequest']
type PasswordChangeRequest = components['schemas']['PasswordChangeRequest']
type ForgotPasswordRequest = components['schemas']['ForgotPasswordRequest']
type PasswordResetRequest = components['schemas']['PasswordResetRequest']

const SESSION_PATH = '/api/v1/admin/auth/me'
const FORM_HEADERS = { 'Content-Type': 'application/x-www-form-urlencoded' }

function useSessionCache() {
  const queryClient = useQueryClient()
  const { queryKey } = api.queryOptions('get', SESSION_PATH)

  return {
    refresh: () => queryClient.invalidateQueries({ queryKey }),
    clear: () => {
      queryClient.removeQueries({
        predicate: (query) => query.queryKey[1] !== SESSION_PATH,
      })
      return queryClient.resetQueries({ queryKey })
    },
    // Remove sem buscar de novo: quem chamou navega para o login em seguida.
    forget: () => queryClient.clear(),
  }
}

export function useAdminSession() {
  const queryClient = useQueryClient()
  const [expired, setExpired] = useState(false)
  const session = api.useQuery('get', SESSION_PATH, {}, { retry: false })
  const unauthorized = expired || problemStatus(session.error) === 401
  const revoked = unauthorized && session.data !== undefined

  useEffect(() => {
    if (revoked) queryClient.clear()
  }, [revoked, queryClient])

  useEffect(() => {
    const expire = () => {
      setExpired(true)
      queryClient.clear()
    }
    adminSessionEvents.addEventListener('expired', expire)
    return () => adminSessionEvents.removeEventListener('expired', expire)
  }, [queryClient])

  return {
    data: unauthorized ? undefined : session.data,
    error: expired ? { status: 401 } : session.error,
    isPending: session.isPending,
  }
}

export function useLogin() {
  const session = useSessionCache()
  const login = api.useMutation('post', '/api/v1/admin/auth/login', {
    onSuccess: session.refresh,
  })

  return {
    error: login.error,
    isPending: login.isPending,
    submit: (credentials: LoginRequest) =>
      login.mutate({ body: credentials, headers: FORM_HEADERS }),
  }
}

export function useLogout() {
  const session = useSessionCache()
  const logout = api.useMutation('post', '/api/v1/admin/auth/logout', {
    onSettled: session.clear,
  })

  return {
    isPending: logout.isPending,
    submit: () => logout.mutate({}),
  }
}

export function useChangePassword(onChanged: () => void) {
  const session = useSessionCache()
  const change = api.useMutation('post', '/api/v1/admin/auth/password', {
    onSuccess: () => {
      session.forget()
      onChanged()
    },
  })

  return {
    error: change.error,
    isPending: change.isPending,
    submit: (passwords: PasswordChangeRequest) =>
      change.mutate({ body: passwords }),
  }
}

export function useRequestPasswordRecovery() {
  const recovery = api.useMutation('post', '/api/v1/admin/auth/password/forgot')

  return {
    error: recovery.error,
    isPending: recovery.isPending,
    isSuccess: recovery.isSuccess,
    submit: (request: ForgotPasswordRequest) =>
      recovery.mutate({ body: request }),
  }
}

export function useResetPassword(onReset: () => void) {
  const reset = api.useMutation('post', '/api/v1/admin/auth/password/reset', {
    onSuccess: onReset,
  })

  return {
    error: reset.error,
    isPending: reset.isPending,
    submit: (request: PasswordResetRequest) => reset.mutate({ body: request }),
  }
}
