import { useState } from 'react'
import { Button } from '../../shared/ui/Button.tsx'
import styles from './SpaceOptionCard.module.css'
import type { SpaceOption } from './spaceOptions.ts'

type SpaceOptionCardProps = {
  option: SpaceOption
  onChoose: () => void
}

export function SpaceOptionCard({ option, onChoose }: SpaceOptionCardProps) {
  const [failedPhoto, setFailedPhoto] = useState<string>()
  const photo = option.photoUrl === failedPhoto ? undefined : option.photoUrl
  return (
    <li className={styles.card}>
      <img
        className={photo ? styles.photo : styles.illustration}
        src={photo ?? option.illustration}
        alt={photo ? `Foto de ${option.name}` : option.illustrationAlt}
        loading="lazy"
        referrerPolicy="no-referrer"
        onError={photo ? () => setFailedPhoto(photo) : undefined}
      />
      <h2 className={styles.name}>{option.name}</h2>
      <p className={styles.description}>{option.description}</p>
      <p className={styles.tag}>{option.tag}</p>
      <Button onClick={onChoose}>Escolher {option.name}</Button>
    </li>
  )
}
