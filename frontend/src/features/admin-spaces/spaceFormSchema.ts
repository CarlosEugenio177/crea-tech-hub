import { z } from 'zod'

const durationMinutes = z
  .string()
  .refine(
    (value) =>
      value === '' ||
      (/^\d+$/.test(value) && Number(value) >= 1 && Number(value) <= 1439),
    'Informe uma duração inteira entre 1 e 1439 minutos.',
  )
  .transform((value) => (value === '' ? undefined : Number(value)))

export const spaceFormSchema = z
  .object({
    name: z
      .string()
      .trim()
      .min(1, 'Informe o nome do espaço.')
      .max(150, 'O nome deve ter até 150 caracteres.'),
    type: z.enum(
      ['ROOM', 'SHARED_TABLE', 'WORKSTATION'],
      'Informe o tipo do espaço.',
    ),
    capacity: z
      .number('Informe a capacidade do espaço.')
      .int('A capacidade deve ser um número inteiro.')
      .positive('A capacidade deve ser maior que zero.'),
    description: z
      .string()
      .trim()
      .max(2000, 'A descrição deve ter até 2000 caracteres.'),
    minDuration: durationMinutes,
    maxDuration: durationMinutes,
    photoUrl: z
      .string()
      .trim()
      .max(2048)
      .refine((value) => {
        if (!value) return true
        try {
          const url = new URL(value)
          return url.protocol === 'https:' && !url.username && !url.password
        } catch {
          return false
        }
      }, 'Informe um link HTTPS válido para a foto.'),
  })
  .refine((space) => space.type !== 'WORKSTATION' || space.capacity === 1, {
    path: ['capacity'],
    message: 'Uma estação individual deve ter capacidade para uma pessoa.',
  })

  .refine(
    ({ minDuration, maxDuration }) =>
      minDuration === undefined ||
      maxDuration === undefined ||
      minDuration <= maxDuration,
    {
      path: ['maxDuration'],
      message: 'A duração máxima deve ser maior ou igual à mínima.',
    },
  )
