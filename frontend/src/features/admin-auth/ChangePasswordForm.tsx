import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router'
import { z } from 'zod'
import { problemMessage } from '../../shared/api/problem'
import { Button } from '../../shared/ui/Button.tsx'
import { FormCard } from '../../shared/ui/FormCard.tsx'
import { Notice } from '../../shared/ui/Notice.tsx'
import { TextField } from '../../shared/ui/TextField.tsx'
import { useChangePassword } from './adminAuthApi.ts'
import { goToLoginWithNotice } from './adminRoutes.ts'
import {
  newPasswordFields,
  PASSWORD_HINT,
  passwordMismatch,
  passwordsMatch,
} from './passwordRules.ts'

const schema = z
  .object({
    currentPassword: z.string().min(1, 'Informe a senha atual.'),
    ...newPasswordFields,
  })
  .refine(passwordsMatch, passwordMismatch)

export function ChangePasswordForm() {
  const navigate = useNavigate()
  const change = useChangePassword(() =>
    goToLoginWithNotice(
      navigate,
      'Senha alterada. Entre novamente com a nova senha.',
    ),
  )
  const { register, handleSubmit, formState } = useForm({
    resolver: zodResolver(schema),
  })

  const submit = handleSubmit(({ currentPassword, newPassword }) =>
    change.mutate({ body: { currentPassword, newPassword } }),
  )

  return (
    <FormCard onSubmit={submit}>
      {change.error && (
        <Notice tone="error">{problemMessage(change.error)}</Notice>
      )}
      <TextField
        label="Senha atual"
        type="password"
        autoComplete="current-password"
        error={formState.errors.currentPassword?.message}
        {...register('currentPassword')}
      />
      <TextField
        label="Nova senha"
        type="password"
        autoComplete="new-password"
        hint={PASSWORD_HINT}
        error={formState.errors.newPassword?.message}
        {...register('newPassword')}
      />
      <TextField
        label="Confirmar nova senha"
        type="password"
        autoComplete="new-password"
        error={formState.errors.confirmation?.message}
        {...register('confirmation')}
      />
      <Button type="submit" disabled={change.isPending}>
        {change.isPending ? 'Salvando…' : 'Alterar senha'}
      </Button>
    </FormCard>
  )
}
