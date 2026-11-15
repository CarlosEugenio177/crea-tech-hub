import createFetchClient, { type Middleware } from 'openapi-fetch'
import createQueryHooks from 'openapi-react-query'
import type { paths } from './schema'

const SAFE_METHODS = ['GET', 'HEAD', 'OPTIONS']

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

apiClient.use(csrf)

export const api = createQueryHooks(apiClient)
