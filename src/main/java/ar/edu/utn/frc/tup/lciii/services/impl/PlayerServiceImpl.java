package ar.edu.utn.frc.tup.lciii.services.impl;

import ar.edu.utn.frc.tup.lciii.dtos.player.NewPlayerRequestDTO;
import ar.edu.utn.frc.tup.lciii.dtos.player.PlayerResponseDTO;
import ar.edu.utn.frc.tup.lciii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.lciii.models.Player;
import ar.edu.utn.frc.tup.lciii.repositories.jpa.PlayerJpaRepository;
import ar.edu.utn.frc.tup.lciii.services.PlayerService;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class PlayerServiceImpl implements PlayerService {

    @Autowired
    private PlayerJpaRepository playerJpaRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public Player getPlayerById(Long id) {
        PlayerEntity playerEntity = playerJpaRepository.getReferenceById(id);
        if(Objects.isNull(playerEntity.getUserName())) {
            throw new EntityNotFoundException();
        }
        return modelMapper.map(playerEntity, Player.class);
    }

    @Override
    public PlayerResponseDTO getPlayerResponseDTOById(Long id) {
        PlayerEntity playerEntity = playerJpaRepository.getReferenceById(id);
        if(Objects.isNull(playerEntity.getUserName())) {
            throw new EntityNotFoundException();
        }
        return modelMapper.map(playerEntity, PlayerResponseDTO.class);
    }

    @Override
    public Player updatePlayerBalance(Player player, BigDecimal newBalance) {
        PlayerEntity playerEntity = playerJpaRepository.findById(player.getId())
                .orElseThrow(EntityNotFoundException::new);
        playerEntity.setBalance(newBalance);
        playerJpaRepository.save(playerEntity);
        return modelMapper.map(playerEntity, Player.class);
    }

    @Override
    public PlayerResponseDTO getPlayerByUserNameAndPassword(String userName, String password) {
        PlayerEntity playerEntity = playerJpaRepository.findByUserNameAndPassword(userName, password)
                .orElseThrow(() -> new EntityNotFoundException("Username or password invalid!"));
        return modelMapper.map(playerEntity, PlayerResponseDTO.class);
    }

    @Override
    public PlayerResponseDTO savePlayer(NewPlayerRequestDTO newPlayerRequestDTO) {
        PlayerEntity playerEntity = modelMapper.map(newPlayerRequestDTO, PlayerEntity.class);
        playerEntity.setBalance(new BigDecimal(200));
        playerJpaRepository.save(playerEntity);
        return modelMapper.map(playerEntity, PlayerResponseDTO.class);
    }

}
