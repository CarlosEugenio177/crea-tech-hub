import { Link } from 'react-router'
import { Notice } from '../../shared/ui/Notice.tsx'
import { PageHeader } from '../../shared/ui/PageHeader.tsx'
import { ADMIN_FORGOT_PASSWORD } from './adminRoutes.ts'
import styles from './AuthPage.module.css'
import { ResetPasswordForm } from './ResetPasswordForm.tsx'
import { useResetToken } from './useResetToken.ts'

export function ResetPasswordPage() {
  const token = useResetToken()

  return (
    <div className={styles.page}>
      <PageHeader
        eyebrow="Mútua · Administração"
        title="Redefinir senha"
        description="Escolha uma nova senha para a sua conta administrativa."
      />
      {token ? (
        <ResetPasswordForm token={token} />
      ) : (
        <Notice tone="error">
          Este link de redefinição é inválido ou está incompleto.
        </Notice>
      )}
      <Link className={styles.link} to={ADMIN_FORGOT_PASSWORD}>
        Solicitar um novo link
      </Link>
    </div>
  )
}
