package ar.edu.utn.frc.tup.lciii.services.impl;

import ar.edu.utn.frc.tup.lciii.dtos.player.PlayerResponseDTO;
import ar.edu.utn.frc.tup.lciii.models.Player;
import ar.edu.utn.frc.tup.lciii.services.LoginService;
import ar.edu.utn.frc.tup.lciii.services.PlayerService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public Player login(String userName, String password) {
        PlayerResponseDTO playerResponse = playerService.getPlayerByUserNameAndPassword(userName, password);
        if (playerResponse != null) {
            return modelMapper.map(playerResponse, Player.class);
        } else {
            throw new IllegalArgumentException("Usuario o contraseña invalidos");
        }
    }
}
