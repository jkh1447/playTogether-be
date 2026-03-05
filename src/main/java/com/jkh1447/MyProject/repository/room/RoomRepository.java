package com.jkh1447.MyProject.repository.room;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jkh1447.MyProject.domain.chating.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {
  
  Boolean existsByRoomId(String roomId);
  
}
