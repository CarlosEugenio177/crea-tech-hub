import { z } from 'zod'

export const newPasswordFields = {
  newPassword: z
    .string()
    .min(12, 'Use pelo menos 12 caracteres.')
    .max(128, 'Use no máximo 128 caracteres.'),
  confirmation: z.string(),
}

export function passwordsMatch(values: {
  newPassword: string
  confirmation: string
}) {
  return values.newPassword === values.confirmation
}

export const passwordMismatch = {
  path: ['confirmation'],
  message: 'As senhas não coincidem.',
}

export const PASSWORD_HINT = 'De 12 a 128 caracteres.'
