import { useState } from 'react'
import { Button } from '../../shared/ui/Button.tsx'
import type { PublicSpace } from './publicSpacesApi.ts'
import styles from './SpaceOptionCard.module.css'
import { spaceDetails } from './spaceOptions.ts'

type SpaceOptionCardProps = {
  space: PublicSpace
  onChoose: () => void
}

export function SpaceOptionCard({ space, onChoose }: SpaceOptionCardProps) {
  const [failedPhoto, setFailedPhoto] = useState<string>()
  const photo =
    space.photoUrl === failedPhoto ? undefined : (space.photoUrl ?? undefined)

  return (
    <li className={photo ? `${styles.room} ${styles.withPhoto}` : styles.room}>
      {photo && (
        <img
          className={styles.photo}
          src={photo}
          alt={`Foto de ${space.name}`}
          loading="lazy"
          referrerPolicy="no-referrer"
          onError={() => setFailedPhoto(photo)}
        />
      )}
      <div className={styles.details}>
        <h2>{space.name}</h2>
        <p className={styles.capacity}>{spaceDetails(space)}</p>
        {space.description && (
          <p className={styles.description}>{space.description}</p>
        )}
        <Button onClick={onChoose} aria-label={`Ver horários de ${space.name}`}>
          Ver horários
        </Button>
      </div>
    </li>
  )
}
