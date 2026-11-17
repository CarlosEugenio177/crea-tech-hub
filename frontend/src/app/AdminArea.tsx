import { Navigate } from 'react-router'
import { useAdminSession } from '../features/admin-auth/adminAuthApi.ts'
import { ADMIN_LOGIN } from '../features/admin-auth/adminRoutes.ts'
import { FirstAccessPage } from '../features/admin-auth/FirstAccessPage.tsx'
import { problemStatus } from '../shared/api/problem.ts'
import { QueryStatus } from '../shared/ui/QueryStatus.tsx'
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
      <QueryStatus error={session.error} />
    </PublicLayout>
  )
}
