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
import { EditSpacePage } from '../features/admin-spaces/EditSpacePage.tsx'
import { NewSpacePage } from '../features/admin-spaces/NewSpacePage.tsx'
import {
  ADMIN_SPACE_EDIT,
  ADMIN_SPACE_LAYOUT,
  ADMIN_SPACE_NEW,
  ADMIN_SPACES,
} from '../features/admin-spaces/spaceRoutes.ts'
import { SpacesPage } from '../features/admin-spaces/SpacesPage.tsx'
import { RoomLayoutPage } from '../features/admin-spaces/layout/RoomLayoutPage.tsx'
import { AuditPage } from '../features/admin-team/AuditPage.tsx'
import { EditPermissionsPage } from '../features/admin-team/EditPermissionsPage.tsx'
import { NewMemberPage } from '../features/admin-team/NewMemberPage.tsx'
import { TeamPage } from '../features/admin-team/TeamPage.tsx'
import {
  ADMIN_TEAM,
  ADMIN_TEAM_AUDIT,
  ADMIN_TEAM_MEMBER,
  ADMIN_TEAM_NEW,
} from '../features/admin-team/teamRoutes.ts'
import { AdminReservationsPage } from '../features/reservation/AdminReservationsPage.tsx'
import { ChooseSpacePage } from '../features/reservation/ChooseSpacePage.tsx'
import { ChooseTimePage } from '../features/reservation/ChooseTimePage.tsx'
import { RequesterPage } from '../features/reservation/RequesterPage.tsx'
import { ReservationFlow } from '../features/reservation/ReservationFlow.tsx'
import {
  ADMIN_RESERVATIONS,
  RESERVE,
  RESERVE_REQUESTER,
  RESERVE_REVIEW,
  RESERVE_TIME,
  TRACKING,
} from '../features/reservation/reservationRoutes.ts'
import { ReviewPage } from '../features/reservation/ReviewPage.tsx'
import { TrackingPage } from '../features/reservation/TrackingPage.tsx'
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
        <Route path={RESERVE} element={<ReservationFlow />}>
          <Route index element={<ChooseSpacePage />} />
          <Route path={RESERVE_TIME} element={<ChooseTimePage />} />
          <Route path={RESERVE_REQUESTER} element={<RequesterPage />} />
          <Route path={RESERVE_REVIEW} element={<ReviewPage />} />
        </Route>
        <Route path={TRACKING} element={<TrackingPage />} />
        <Route path={ADMIN_LOGIN} element={<LoginPage />} />
        <Route path={ADMIN_FORGOT_PASSWORD} element={<ForgotPasswordPage />} />
        <Route path={PASSWORD_RESET} element={<ResetPasswordPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
      <Route path={ADMIN_HOME} element={<AdminArea />}>
        <Route index element={<AdminHomePage />} />
        <Route path={ADMIN_RESERVATIONS} element={<AdminReservationsPage />} />
        <Route path={ADMIN_SPACES} element={<SpacesPage />} />
        <Route path={ADMIN_SPACE_NEW} element={<NewSpacePage />} />
        <Route path={ADMIN_SPACE_EDIT} element={<EditSpacePage />} />
        <Route path={ADMIN_SPACE_LAYOUT} element={<RoomLayoutPage />} />
        <Route path={ADMIN_TEAM} element={<TeamPage />} />
        <Route path={ADMIN_TEAM_NEW} element={<NewMemberPage />} />
        <Route path={ADMIN_TEAM_AUDIT} element={<AuditPage />} />
        <Route path={ADMIN_TEAM_MEMBER} element={<EditPermissionsPage />} />
        <Route path={ADMIN_PASSWORD} element={<ChangePasswordPage />} />
      </Route>
    </Routes>
  )
}
