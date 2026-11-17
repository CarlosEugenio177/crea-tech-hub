import { afterEach, expect, test, vi } from 'vitest'
import { apiClient } from './client.ts'

afterEach(() => vi.unstubAllGlobals())

test.each([null, '<html>Service unavailable</html>'])(
  'normalizes an unavailable backend response: %s',
  async (body) => {
    vi.stubGlobal('fetch', async () => new Response(body, { status: 503 }))
    const response = await apiClient.GET('/api/v1/spaces')
    expect(response.error).toMatchObject({
      status: 503,
      detail: 'Não foi possível concluir a solicitação. Tente novamente.',
    })
    expect(response.data).toBeUndefined()
  },
)

test('preserves structured validation errors from the backend', async () => {
  const problem = {
    status: 400,
    detail: 'Verifique os campos informados.',
    errors: [{ field: 'name', message: 'Informe o nome.' }],
  }
  vi.stubGlobal('fetch', async () => Response.json(problem, { status: 400 }))
  const response = await apiClient.GET('/api/v1/spaces')
  expect(response.error).toEqual(problem)
})
