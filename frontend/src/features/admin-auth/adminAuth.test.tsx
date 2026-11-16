import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, expect, test, vi } from 'vitest'
import {
  CSRF_HEADER,
  CSRF_TOKEN,
  fakeApi,
  json,
  noContent,
  problem,
} from '../../test/fakeApi.ts'
import { renderApp } from '../../test/renderApp.tsx'

const SESSION = 'GET /api/v1/admin/auth/me'
const LOGIN = 'POST /api/v1/admin/auth/login'
const PASSWORD = 'POST /api/v1/admin/auth/password'
const RESET = 'POST /api/v1/admin/auth/password/reset'

const unauthenticated = () => problem(401, 'Autenticação necessária.')
const account = (passwordChangeRequired = false) =>
  json({
    id: '0b9f6f0e-7d0c-4c53-9d0a-3f1f5f0f8a11',
    fullName: 'Ana Gestora',
    email: 'ana@example.com',
    role: 'ADMIN',
    permissions: ['MANAGE_ACCESS'],
    passwordChangeRequired,
  })

afterEach(() => {
  vi.unstubAllGlobals()
  window.location.hash = ''
})

test('sends visitors without a session from the admin area to the login', async () => {
  fakeApi({ [SESSION]: unauthenticated })

  renderApp('/admin')

  expect(
    await screen.findByRole('heading', { name: 'Acesso da equipe' }),
  ).toBeInTheDocument()
})

test('logs in with form-encoded credentials and the CSRF token', async () => {
  const user = userEvent.setup()
  const api = fakeApi({ [SESSION]: unauthenticated })
  api.routes[LOGIN] = () => {
    api.routes[SESSION] = () => account()
    return noContent()
  }
  renderApp('/admin/entrar')

  await user.type(screen.getByLabelText('E-mail'), 'ana@example.com')
  await user.type(screen.getByLabelText('Senha'), 'senha-muito-segura')
  await user.click(screen.getByRole('button', { name: 'Entrar' }))

  expect(
    await screen.findByRole('heading', { name: 'Visão geral' }),
  ).toBeInTheDocument()
  const [login] = api.sent(LOGIN)
  expect(login.headers.get(CSRF_HEADER)).toBe(CSRF_TOKEN)
  expect(login.headers.get('Content-Type')).toBe(
    'application/x-www-form-urlencoded',
  )
  expect(login.body).toBe('email=ana%40example.com&password=senha-muito-segura')
})

test('shows the backend message when the credentials are rejected', async () => {
  const user = userEvent.setup()
  fakeApi({
    [SESSION]: unauthenticated,
    [LOGIN]: () => problem(401, 'E-mail ou senha inválidos.'),
  })
  renderApp('/admin/entrar')

  await user.type(screen.getByLabelText('E-mail'), 'ana@example.com')
  await user.type(screen.getByLabelText('Senha'), 'senha-errada')
  await user.click(screen.getByRole('button', { name: 'Entrar' }))

  expect(await screen.findByRole('alert')).toHaveTextContent(
    'E-mail ou senha inválidos.',
  )
})

test('requires a personal password before opening the admin area', async () => {
  fakeApi({ [SESSION]: () => account(true) })

  renderApp('/admin')

  expect(
    await screen.findByRole('heading', { name: 'Crie sua senha pessoal' }),
  ).toBeInTheDocument()
  expect(
    screen.queryByRole('navigation', { name: 'Administração' }),
  ).not.toBeInTheDocument()
})

test('rejects a short new password without calling the API', async () => {
  const user = userEvent.setup()
  const api = fakeApi({ [SESSION]: () => account(true) })
  renderApp('/admin')

  await user.type(await screen.findByLabelText('Senha atual'), 'senha-inicial')
  await user.type(screen.getByLabelText('Nova senha'), 'curta')
  await user.type(screen.getByLabelText('Confirmar nova senha'), 'curta')
  await user.click(screen.getByRole('button', { name: 'Alterar senha' }))

  expect(
    await screen.findByText('Use pelo menos 12 caracteres.'),
  ).toBeInTheDocument()
  expect(api.sent(PASSWORD)).toHaveLength(0)
})

test('returns to the login after the password is changed', async () => {
  const user = userEvent.setup()
  const api = fakeApi({ [SESSION]: () => account(true) })
  api.routes[PASSWORD] = () => {
    api.routes[SESSION] = unauthenticated
    return noContent()
  }
  renderApp('/admin')

  await user.type(await screen.findByLabelText('Senha atual'), 'senha-inicial')
  await user.type(screen.getByLabelText('Nova senha'), 'nova-senha-pessoal')
  await user.type(
    screen.getByLabelText('Confirmar nova senha'),
    'nova-senha-pessoal',
  )
  await user.click(screen.getByRole('button', { name: 'Alterar senha' }))

  expect(await screen.findByRole('status')).toHaveTextContent(
    'Senha alterada. Entre novamente com a nova senha.',
  )
  expect(JSON.parse(api.sent(PASSWORD)[0].body)).toEqual({
    currentPassword: 'senha-inicial',
    newPassword: 'nova-senha-pessoal',
  })
})

test('resets the password with the token from the link fragment', async () => {
  const user = userEvent.setup()
  const token = 'a'.repeat(43)
  const api = fakeApi({ [SESSION]: unauthenticated, [RESET]: noContent })
  window.location.hash = `#token=${token}`
  renderApp('/redefinir-senha')

  expect(window.location.hash).toBe('')
  await user.type(screen.getByLabelText('Nova senha'), 'nova-senha-pessoal')
  await user.type(
    screen.getByLabelText('Confirmar nova senha'),
    'nova-senha-pessoal',
  )
  await user.click(screen.getByRole('button', { name: 'Redefinir senha' }))

  expect(await screen.findByRole('status')).toHaveTextContent(
    'Senha redefinida. Entre com a nova senha.',
  )
  expect(JSON.parse(api.sent(RESET)[0].body)).toEqual({
    token,
    newPassword: 'nova-senha-pessoal',
  })
})

test('explains when the reset link has no valid token', () => {
  fakeApi({})

  renderApp('/redefinir-senha')

  expect(screen.getByRole('alert')).toHaveTextContent(
    'Este link de redefinição é inválido ou está incompleto.',
  )
  expect(
    screen.queryByRole('button', { name: 'Redefinir senha' }),
  ).not.toBeInTheDocument()
})
