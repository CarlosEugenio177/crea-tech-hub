import type { NavigateFunction } from 'react-router'

export const ADMIN_HOME = '/admin'
export const ADMIN_LOGIN = '/admin/entrar'
export const ADMIN_PASSWORD = '/admin/senha'
export const ADMIN_FORGOT_PASSWORD = '/admin/esqueci-senha'
export const PASSWORD_RESET = '/redefinir-senha'

export function goToLoginWithNotice(
  navigate: NavigateFunction,
  notice: string,
) {
  void navigate(ADMIN_LOGIN, { replace: true, state: { notice } })
}

export function readLoginNotice(state: unknown): string | undefined {
  if (typeof state !== 'object' || state === null || !('notice' in state)) {
    return undefined
  }
  return typeof state.notice === 'string' ? state.notice : undefined
}
