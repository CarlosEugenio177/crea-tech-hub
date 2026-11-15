import { Route, Routes } from 'react-router'
import {
  ADMIN_FORGOT_PASSWORD,
  ADMIN_HOME,
  ADMIN_LOGIN,
  ADMIN_PASSWORD,
  PASSWORD_RESET,
} from '../features/admin-auth/adminRoutes.ts'
import { ChangePasswordPage } from '../features/admin-auth/ChangePasswordPage.tsx'
import { ForgotPasswordPage } from '../features/admin-auth/ForgotPasswordPage.tsx'
import { LoginPage } from '../features/admin-auth/LoginPage.tsx'
import { ResetPasswordPage } from '../features/admin-auth/ResetPasswordPage.tsx'
import { AdminArea } from './AdminArea.tsx'
import { AdminHomePage } from './AdminHomePage.tsx'
import { HomePage } from './HomePage.tsx'
import { NotFoundPage } from './NotFoundPage.tsx'
import { PublicLayout } from './PublicLayout.tsx'

export function App() {
  return (
    <Routes>
      <Route element={<PublicLayout />}>
        <Route index element={<HomePage />} />
        <Route path={ADMIN_LOGIN} element={<LoginPage />} />
        <Route path={ADMIN_FORGOT_PASSWORD} element={<ForgotPasswordPage />} />
        <Route path={PASSWORD_RESET} element={<ResetPasswordPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
      <Route path={ADMIN_HOME} element={<AdminArea />}>
        <Route index element={<AdminHomePage />} />
        <Route path={ADMIN_PASSWORD} element={<ChangePasswordPage />} />
      </Route>
    </Routes>
  )
}
