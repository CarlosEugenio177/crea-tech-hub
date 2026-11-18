import { z } from 'zod'
import { isValidCpf } from './cpf.ts'

export const PROFILE_LABELS = {
  MUTUALIST: 'Mutualista / profissional do Crea',
  EXTERNAL: 'Público externo',
}

export const requesterSchema = z
  .object({
    profile: z.enum(['MUTUALIST', 'EXTERNAL'], 'Informe seu perfil.'),
    fullName: z.string().trim().min(1, 'Informe seu nome completo.').max(150),
    cpf: z.string().trim().refine(isValidCpf, 'Informe um CPF válido.'),
    whatsapp: z
      .string()
      .trim()
      .refine(
        (value) => /^\d{10,11}$/.test(value.replace(/\D/g, '')),
        'Informe o WhatsApp com DDD.',
      ),
    email: z.email('Informe um e-mail válido.').max(254),
    onBehalfOfOthers: z.boolean(),
    reason: z.string().trim().max(500),
    notes: z.string().trim().max(1000),
    rulesAccepted: z.boolean(),
  })
  .refine(
    (requester) =>
      requester.reason !== '' ||
      (requester.profile !== 'EXTERNAL' && !requester.onBehalfOfOthers),
    { path: ['reason'], message: 'Informe o motivo da reserva.' },
  )
  .refine((requester) => requester.rulesAccepted, {
    path: ['rulesAccepted'],
    message: 'Aceite as regras de uso para continuar.',
  })

export type Requester = z.infer<typeof requesterSchema>
