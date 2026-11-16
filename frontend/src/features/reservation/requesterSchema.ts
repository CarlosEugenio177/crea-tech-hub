import { z } from 'zod'

export const PROFILE_LABELS = {
  MUTUALIST: 'Mutualista / profissional do Crea',
  EXTERNAL: 'Público externo',
}

export const requesterSchema = z
  .object({
    profile: z.enum(['MUTUALIST', 'EXTERNAL'], 'Informe seu perfil.'),
    fullName: z.string().trim().min(1, 'Informe seu nome completo.'),
    mutuaId: z.string().trim(),
    whatsapp: z
      .string()
      .trim()
      .refine(
        (value) => /^\d{10,11}$/.test(value.replace(/\D/g, '')),
        'Informe o WhatsApp com DDD.',
      ),
    email: z.email('Informe um e-mail válido.'),
    onBehalfOfOthers: z.boolean(),
    reason: z.string().trim(),
    notes: z.string().trim(),
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
