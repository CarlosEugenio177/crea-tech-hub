import { PageHeader } from '../../shared/ui/PageHeader.tsx'
import styles from './AuthPage.module.css'
import { ChangePasswordForm } from './ChangePasswordForm.tsx'

export function ChangePasswordPage() {
  return (
    <div className={styles.page}>
      <PageHeader
        eyebrow="Administração · Conta"
        title="Alterar senha"
        description="Ao alterar a senha, todas as sessões são encerradas e é preciso entrar novamente."
      />
      <ChangePasswordForm />
    </div>
  )
}
