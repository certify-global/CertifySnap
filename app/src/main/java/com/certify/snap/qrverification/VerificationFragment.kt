/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
 *  ---
 *  Copyright (C) 2021 T-Systems International GmbH and all other contributors
 *  ---
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  ---license-end
 *
 *  Created by osarapulov on 9/2/21 11:07 AM
 */

package com.certify.snap.qrverification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.certify.snap.databinding.FragmentVerificationBinding
import dgca.verifier.app.android.model.rules.RuleValidationResultModelsContainer

class VerificationFragment : Fragment() {
    private val viewModel by viewModels<VerificationViewModel>()
  //  private val args by navArgs<VerificationFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.init("HC1:6BF%RN%TSMAHN-H+UOJPPMYDOMP83S:D4/*G%CM5:KFY0.TMUY4.\$RMGJ7NP::QQHIZC4TPIFRMLNKNM8POCEUG*%NH\$RTNP8EFKHL::H*:JSC9VFFM.9.GJ5HFLF95HFX*5:\$CWC51FD\$W4R.9D0KU0GHJP7NVDEBK3JG.8O%0CNNA*4W\$C2VLHOP+MMBT16Y51Y9AT1 %PZIEQKERQ8IY1I\$HH%U8 9PS5/IE%TE6UG+ZEAT1HQ13W1:O14SINTUWL7N586IASD9YHILIIX2MZJK6HIYIA FE5XHLJDEJCXC95KD%FHA0G6LG9KDG+990HY2DYJDRJC H9 KE7*G%9DJ6K1AD1WMN+IAJK%8LC-ISC8OHG*50/43GYN77H4F7PHBM*4CZKHKB-43.E3KD3OAJSZ4P:4:NN8X2VEOS*HCBBMMD6POB-9CH2C-CSPI%UIJUIM+O3FU9ME2W1Y.96\$F/TVIEPCQAHJB+UVXP6%27O-E4*0H67+:M1/SM9JK/319T58UJXKU*D2IMZ50000FGWF573SF","")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentVerificationBinding.inflate(inflater, container, false).root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.qrCodeVerificationResult.observe(viewLifecycleOwner) { qrCodeVerificationResult ->
            if (qrCodeVerificationResult is QrCodeVerificationResult.Applicable) {
                setFragmentResult(
                    VERIFY_REQUEST_KEY,
                    bundleOf(
                        STANDARDISED_VERIFICATION_RESULT_KEY to qrCodeVerificationResult.standardizedVerificationResult,
                        CERTIFICATE_MODEL_KEY to qrCodeVerificationResult.certificateModel,
                        HCERT_KEY to qrCodeVerificationResult.hcert,
                        RULE_VALIDATION_RESULT_MODELS_CONTAINER_KEY to qrCodeVerificationResult.rulesValidationResults?.let {
                            RuleValidationResultModelsContainer(it)
                        },
                        IS_DEBUG_MODE_ENABLED to qrCodeVerificationResult.isDebugModeEnabled,
                        DEBUG_DATA to qrCodeVerificationResult.debugData
                    )
                )
            }
        }
    }

}