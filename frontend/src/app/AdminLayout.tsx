import { NavLink, Outlet } from 'react-router'
import { useLogout } from '../features/admin-auth/adminAuthApi.ts'
import type { AdminAccount } from '../features/admin-auth/adminAccount.ts'
import { ADMIN_HOME } from '../features/admin-auth/adminRoutes.ts'
import { Brand } from '../shared/ui/Brand.tsx'
import styles from './AdminLayout.module.css'
import { adminMenuFor } from './adminMenu.ts'

type AdminLayoutProps = { account: AdminAccount }

export function AdminLayout({ account }: AdminLayoutProps) {
  const logout = useLogout()

  return (
    <div className={styles.shell}>
      <aside className={styles.sidebar}>
        <Brand to={ADMIN_HOME} variant="positive" />
        <nav className={styles.nav} aria-label="Administração">
          {adminMenuFor(account).map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === ADMIN_HOME}
              className={({ isActive }) =>
                isActive ? styles.activeItem : styles.item
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className={styles.account}>
          <p className={styles.accountName}>{account.fullName}</p>
          <p className={styles.accountEmail}>{account.email}</p>
          <button
            type="button"
            className={styles.logout}
            disabled={logout.isPending}
            onClick={logout.submit}
          >
            Sair
          </button>
        </div>
      </aside>
      <main className={styles.content}>
        <Outlet context={account} />
      </main>
    </div>
  )
}
