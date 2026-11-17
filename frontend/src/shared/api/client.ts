import createFetchClient, { type Middleware } from 'openapi-fetch'
import createQueryHooks from 'openapi-react-query'
import type { paths } from './schema'

const SAFE_METHODS = ['GET', 'HEAD', 'OPTIONS']

export const adminSessionEvents = new EventTarget()

export const apiClient = createFetchClient<paths>({
  baseUrl: window.location.origin,
  credentials: 'same-origin',
  fetch: (request) => globalThis.fetch(request),
})

// O token CSRF muda com a sessão, por isso é buscado antes de cada escrita.
const csrf: Middleware = {
  async onRequest({ request }) {
    if (SAFE_METHODS.includes(request.method)) return request

    const { data } = await apiClient.GET('/api/v1/admin/auth/csrf')
    if (data) request.headers.set(data.headerName, data.token)
    return request
  },
}

const sessionGuard: Middleware = {
  onResponse({ request, response }) {
    const path = new URL(request.url).pathname
    if (
      response.status === 401 &&
      path.startsWith('/api/v1/admin/') &&
      path !== '/api/v1/admin/auth/login' &&
      path !== '/api/v1/admin/auth/me'
    ) {
      adminSessionEvents.dispatchEvent(new Event('expired'))
    }
    return response
  },
}

apiClient.use(csrf, sessionGuard)

export const api = createQueryHooks(apiClient)
