import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router'
import { z } from 'zod'
import { Button } from '../../shared/ui/Button.tsx'
import { FormCard } from '../../shared/ui/FormCard.tsx'
import { Notice } from '../../shared/ui/Notice.tsx'
import { PageHeader } from '../../shared/ui/PageHeader.tsx'
import { ProblemNotice } from '../../shared/ui/ProblemNotice.tsx'
import { TextField } from '../../shared/ui/TextField.tsx'
import { useRequestPasswordRecovery } from './adminAuthApi.ts'
import { ADMIN_LOGIN } from './adminRoutes.ts'
import styles from './AuthPage.module.css'

const schema = z.object({ email: z.email('Informe um e-mail válido.') })

export function ForgotPasswordPage() {
  const recovery = useRequestPasswordRecovery()
  const { register, handleSubmit, formState } = useForm({
    resolver: zodResolver(schema),
  })

  return (
    <div className={styles.page}>
      <PageHeader
        eyebrow="Mútua · Administração"
        title="Recuperar acesso"
        description="Informe o e-mail da sua conta para receber o link de redefinição de senha."
      />
      <FormCard onSubmit={handleSubmit(recovery.submit)}>
        {recovery.isSuccess && (
          <Notice tone="success">
            Se houver uma conta ativa com este e-mail, o link de redefinição
            chegará em instantes.
          </Notice>
        )}
        <ProblemNotice error={recovery.error} />
        <TextField
          label="E-mail"
          type="email"
          autoComplete="username"
          placeholder="Seu e-mail de trabalho"
          error={formState.errors.email?.message}
          {...register('email')}
        />
        <Button type="submit" disabled={recovery.isPending}>
          {recovery.isPending ? 'Enviando…' : 'Enviar link'}
        </Button>
      </FormCard>
      <Link className={styles.link} to={ADMIN_LOGIN}>
        Voltar para o acesso
      </Link>
    </div>
  )
}
