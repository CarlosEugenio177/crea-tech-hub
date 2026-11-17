import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router'
import { z } from 'zod'
import { Button } from '../../shared/ui/Button.tsx'
import { FormCard } from '../../shared/ui/FormCard.tsx'
import { ProblemNotice } from '../../shared/ui/ProblemNotice.tsx'
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
  const reset = useResetPassword(() =>
    goToLoginWithNotice(navigate, 'Senha redefinida. Entre com a nova senha.'),
  )
  const { register, handleSubmit, formState } = useForm({
    resolver: zodResolver(schema),
  })

  const submit = handleSubmit(({ newPassword }) =>
    reset.submit({ token, newPassword }),
  )

  return (
    <FormCard onSubmit={submit}>
      <ProblemNotice error={reset.error} />
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
