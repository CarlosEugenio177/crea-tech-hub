import { PageHeader } from '../../shared/ui/PageHeader.tsx'
import { useLogout } from './adminAuthApi.ts'
import styles from './AuthPage.module.css'
import { ChangePasswordForm } from './ChangePasswordForm.tsx'

export function FirstAccessPage() {
  const logout = useLogout()

  return (
    <div className={styles.page}>
      <PageHeader
        eyebrow="Mútua · Primeiro acesso"
        title="Crie sua senha pessoal"
        description="Substitua a senha inicial recebida do gestor para liberar a área administrativa."
      />
      <ChangePasswordForm />
      <button
        type="button"
        className={styles.linkButton}
        onClick={() => logout.mutate({})}
      >
        Sair
      </button>
    </div>
  )
}
