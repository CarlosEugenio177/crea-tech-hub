import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router'
import { z } from 'zod'
import { problemMessage } from '../../shared/api/problem'
import { Button } from '../../shared/ui/Button.tsx'
import { FormCard } from '../../shared/ui/FormCard.tsx'
import { Notice } from '../../shared/ui/Notice.tsx'
import { TextField } from '../../shared/ui/TextField.tsx'
import { useResetPassword } from './adminAuthApi.ts'
import { goToLoginWithNotice } from './adminRoutes.ts'
import {
  newPasswordFields,
  PASSWORD_HINT,
  passwordMismatch,
  passwordsMatch,
} from './passwordRules.ts'

const schema = z
  .object(newPasswordFields)
  .refine(passwordsMatch, passwordMismatch)

type ResetPasswordFormProps = { token: string }

export function ResetPasswordForm({ token }: ResetPasswordFormProps) {
  const navigate = useNavigate()
  const reset = useResetPassword()
  const { register, handleSubmit, formState } = useForm({
    resolver: zodResolver(schema),
  })

  const submit = handleSubmit(({ newPassword }) =>
    reset.mutate(
      { body: { token, newPassword } },
      {
        onSuccess: () =>
          goToLoginWithNotice(
            navigate,
            'Senha redefinida. Entre com a nova senha.',
          ),
      },
    ),
  )

  return (
    <FormCard onSubmit={submit}>
      {reset.error && (
        <Notice tone="error">{problemMessage(reset.error)}</Notice>
      )}
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
      <Button type="submit" disabled={reset.isPending}>
        {reset.isPending ? 'Salvando…' : 'Redefinir senha'}
      </Button>
    </FormCard>
  )
}
