import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { problemMessage } from '../../shared/api/problem'
import { Button } from '../../shared/ui/Button.tsx'
import { FormCard } from '../../shared/ui/FormCard.tsx'
import { Notice } from '../../shared/ui/Notice.tsx'
import { TextField } from '../../shared/ui/TextField.tsx'
import { useLogin } from './adminAuthApi.ts'

const schema = z.object({
  email: z.email('Informe um e-mail válido.'),
  password: z.string().min(1, 'Informe a senha.'),
})

type LoginFormProps = { notice?: string }

export function LoginForm({ notice }: LoginFormProps) {
  const login = useLogin()
  const { register, handleSubmit, formState } = useForm({
    resolver: zodResolver(schema),
  })

  return (
    <FormCard onSubmit={handleSubmit(login.submit)}>
      {notice && !login.error && <Notice tone="success">{notice}</Notice>}
      {login.error && (
        <Notice tone="error">{problemMessage(login.error)}</Notice>
      )}
      <TextField
        label="E-mail"
        type="email"
        autoComplete="username"
        placeholder="Seu e-mail de trabalho"
        error={formState.errors.email?.message}
        {...register('email')}
      />
      <TextField
        label="Senha"
        type="password"
        autoComplete="current-password"
        error={formState.errors.password?.message}
        {...register('password')}
      />
      <Button type="submit" disabled={login.isPending}>
        {login.isPending ? 'Entrando…' : 'Entrar'}
      </Button>
    </FormCard>
  )
}
