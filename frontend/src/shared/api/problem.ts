import type { components } from './schema'

type ProblemDetail = components['schemas']['ProblemDetail']

const FALLBACK_MESSAGE =
  'Não foi possível concluir a operação. Tente novamente.'

function isProblemDetail(error: unknown): error is ProblemDetail {
  return typeof error === 'object' && error !== null && 'status' in error
}

export function problemStatus(error: unknown): number | undefined {
  return isProblemDetail(error) ? error.status : undefined
}

export function problemMessage(error: unknown): string {
  return (isProblemDetail(error) && error.detail) || FALLBACK_MESSAGE
}
