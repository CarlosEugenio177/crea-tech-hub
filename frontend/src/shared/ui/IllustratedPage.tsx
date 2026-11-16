import type { ReactNode } from 'react'
import styles from './IllustratedPage.module.css'
import { PageHeader } from './PageHeader.tsx'

type IllustratedPageProps = {
  eyebrow: string
  title: string
  description: string
  illustration: string
  illustrationAlt: string
  children?: ReactNode
}

export function IllustratedPage({
  eyebrow,
  title,
  description,
  illustration,
  illustrationAlt,
  children,
}: IllustratedPageProps) {
  return (
    <div className={styles.page}>
      <div className={styles.content}>
        <PageHeader eyebrow={eyebrow} title={title} description={description} />
        {children}
      </div>
      <img
        className={styles.illustration}
        src={illustration}
        alt={illustrationAlt}
      />
    </div>
  )
}
