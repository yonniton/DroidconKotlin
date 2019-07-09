//
//  AboutView.swift
//  iosApp
//
//  Created by Ben Whitley on 7/2/19.
//  Copyright © 2019 Kevin Galligan. All rights reserved.
//

import SwiftUI
import lib

struct AboutView : View {
    @ObjectBinding var viewModel: AboutViewModel
    
    var body: some View {
        NavigationView {
            List(viewModel.aboutInfo.identified(by: \.title)) { data in
                AboutRow(data: data)
            }
            .onAppear() {
                self.viewModel.fetch()
            }
            .navigationBarTitle(
                Text("About"),
                displayMode: .automatic)
        }
    }
}

struct AboutRow : View {
    var data: AboutInfo
    
    var body: some View {
        VStack(alignment: .leading) {
            HStack {
                Spacer()
                Image(data.icon)
                    .renderingMode(.original)
                Spacer()
            }
            
            Spacer()
            Text(data.title).font(.headline)

            Spacer()
            Text(data.detail).font(.subheadline)
        }
    }
}

#if DEBUG
struct AboutView_Previews : PreviewProvider {
    static var previews: some View {
        AboutView(viewModel: AboutViewModel())
    }
}
#endif

