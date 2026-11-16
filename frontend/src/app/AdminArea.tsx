import { Navigate } from 'react-router'
import { useAdminSession } from '../features/admin-auth/adminAuthApi.ts'
import { ADMIN_LOGIN } from '../features/admin-auth/adminRoutes.ts'
import { FirstAccessPage } from '../features/admin-auth/FirstAccessPage.tsx'
import { problemMessage, problemStatus } from '../shared/api/problem'
import { Notice } from '../shared/ui/Notice.tsx'
import { AdminLayout } from './AdminLayout.tsx'
import { PublicLayout } from './PublicLayout.tsx'

export function AdminArea() {
  const session = useAdminSession()

  if (session.data?.passwordChangeRequired) {
    return (
      <PublicLayout>
        <FirstAccessPage />
      </PublicLayout>
    )
  }
  if (session.data) return <AdminLayout account={session.data} />
  if (problemStatus(session.error) === 401) {
    return <Navigate to={ADMIN_LOGIN} replace />
  }

  return (
    <PublicLayout>
      {session.isPending ? (
        <Notice tone="info">Carregando…</Notice>
      ) : (
        <Notice tone="error">{problemMessage(session.error)}</Notice>
      )}
    </PublicLayout>
  )
}
