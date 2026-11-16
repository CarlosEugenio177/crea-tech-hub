import { Link, Navigate, useLocation } from 'react-router'
import { PageHeader } from '../../shared/ui/PageHeader.tsx'
import { useAdminSession } from './adminAuthApi.ts'
import {
  ADMIN_FORGOT_PASSWORD,
  ADMIN_HOME,
  readLoginNotice,
} from './adminRoutes.ts'
import styles from './AuthPage.module.css'
import { LoginForm } from './LoginForm.tsx'

export function LoginPage() {
  const session = useAdminSession()
  const location = useLocation()

  if (session.data) return <Navigate to={ADMIN_HOME} replace />

  return (
    <div className={styles.page}>
      <PageHeader
        eyebrow="Mútua · Administração"
        title="Acesso da equipe"
        description="Área exclusiva dos funcionários responsáveis pelas reservas."
      />
      <LoginForm notice={readLoginNotice(location.state)} />
      <Link className={styles.link} to={ADMIN_FORGOT_PASSWORD}>
        Esqueci minha senha
      </Link>
    </div>
  )
}
