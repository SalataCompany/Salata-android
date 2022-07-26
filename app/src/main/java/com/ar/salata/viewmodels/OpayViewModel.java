package com.ar.salata.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ar.salata.repositories.OpayRepository;
import com.ar.salata.repositories.UserRepository;
import com.ar.salata.repositories.model.OpaySetting;
import com.ar.salata.repositories.model.Refund;

public class OpayViewModel extends ViewModel {

    private OpayRepository opayRepository = new OpayRepository();

    public MutableLiveData<OpaySetting> getOpaySetting(){
        return opayRepository.getOpaySettings();
    }

    public MutableLiveData<UserRepository.APIResponse> setOpayPaymentInput(int userId, String ref){
        return opayRepository.setOpayPaymentInput(userId, ref);
    }

    public MutableLiveData<Refund> refund(String userId){
        return opayRepository.refund(userId);
    }
}
