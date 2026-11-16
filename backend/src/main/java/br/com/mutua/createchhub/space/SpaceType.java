package br.com.mutua.createchhub.space;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum SpaceType {
  ROOM,
  SHARED_TABLE,
  WORKSTATION
}
