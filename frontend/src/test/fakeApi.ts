import { vi } from 'vitest'

type Handler = () => Response
type RecordedRequest = { route: string; headers: Headers; body: string }

const CSRF_ROUTE = 'GET /api/v1/admin/auth/csrf'

export const CSRF_HEADER = 'X-CSRF-TOKEN'
export const CSRF_TOKEN = 'csrf-de-teste'

export function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

export function problem(status: number, detail: string) {
  return new Response(JSON.stringify({ status, detail }), {
    status,
    headers: { 'Content-Type': 'application/problem+json' },
  })
}

export function noContent() {
  return new Response(null, { status: 204 })
}

export function fakeApi(handlers: Record<string, Handler>) {
  const requests: RecordedRequest[] = []
  const routes: Record<string, Handler> = {
    [CSRF_ROUTE]: () =>
      json({
        headerName: CSRF_HEADER,
        parameterName: '_csrf',
        token: CSRF_TOKEN,
      }),
    ...handlers,
  }

  vi.stubGlobal('fetch', async (request: Request) => {
    const route = `${request.method} ${new URL(request.url).pathname}`
    requests.push({
      route,
      headers: request.headers,
      body: await request.text(),
    })
    const handler = routes[route]
    return handler ? handler() : problem(404, `Rota não simulada: ${route}`)
  })

  return {
    routes,
    sent: (route: string) => requests.filter((r) => r.route === route),
  }
}
