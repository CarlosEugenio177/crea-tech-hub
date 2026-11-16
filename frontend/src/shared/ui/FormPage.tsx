import type { ReactNode } from 'react'
import { Link } from 'react-router'
import styles from './FormPage.module.css'
import { PageHeader } from './PageHeader.tsx'

type FormPageProps = {
  eyebrow: string
  title: string
  description: string
  backTo: string
  backLabel: string
  children: ReactNode
}

export function FormPage({
  eyebrow,
  title,
  description,
  backTo,
  backLabel,
  children,
}: FormPageProps) {
  return (
    <div className={styles.page}>
      <PageHeader eyebrow={eyebrow} title={title} description={description} />
      {children}
      <Link to={backTo} className={styles.link}>
        {backLabel}
      </Link>
    </div>
  )
}
