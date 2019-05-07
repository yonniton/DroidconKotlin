//
//  CrashView.swift
//  iosApp
//
//  Created by Ben Whitley on 5/7/19.
//  Copyright © 2019 Kevin Galligan. All rights reserved.
//

import UIKit
import lib

class CrashView: UIView {

    let viewModel = CrashViewModel()
    
    @IBAction func mainThreadCrash(_ sender: Any) {
        viewModel.forceCrash()
    }
    
    @IBAction func backgroundThreadCrash(_ sender: Any) {
        viewModel.backgroundCrash()
    }
}
