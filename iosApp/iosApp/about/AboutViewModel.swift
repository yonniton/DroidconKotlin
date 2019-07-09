//
//  AboutViewModel.swift
//  iosApp
//
//  Created by Ben Whitley on 7/3/19.
//  Copyright © 2019 Kevin Galligan. All rights reserved.
//

import SwiftUI
import Combine
import lib

typealias EmptyClosure = () -> Void

class AboutViewModel : BindableObject {
    var aboutInfo: [AboutInfo] = [] {
        didSet {
            didChange.send(self)
        }
    }

    var didChange = PassthroughSubject<AboutViewModel, Never>()

    let model: AboutModelSwiftUI
    init() {
        self.model = AboutModelSwiftUI()
    }

    func fetch() {
        model.fetchAboutInfo(success: { data in
            self.aboutInfo = data
            return KotlinUnit()
        })
    }
}
