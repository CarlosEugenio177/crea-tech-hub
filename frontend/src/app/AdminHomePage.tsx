import { Link, useOutletContext } from 'react-router'
import { ADMIN_HOME } from '../features/admin-auth/adminRoutes.ts'
import { PageHeader } from '../shared/ui/PageHeader.tsx'
import styles from './AdminHomePage.module.css'
import { adminMenuFor, type AdminAccount } from './adminMenu.ts'

export function AdminHomePage() {
  const account = useOutletContext<AdminAccount>()
  const areas = adminMenuFor(account).filter((item) => item.to !== ADMIN_HOME)

  return (
    <div className={styles.page}>
      <PageHeader
        eyebrow="Administração"
        title="Visão geral"
        description={`Você entrou como ${account.fullName}.`}
      />
      <ul className={styles.areas}>
        {areas.map((area) => (
          <li key={area.to}>
            <Link to={area.to} className={styles.area}>
              <span className={styles.areaName}>{area.label}</span>
              <span className={styles.areaDescription}>{area.description}</span>
            </Link>
          </li>
        ))}
      </ul>
    </div>
  )
}
