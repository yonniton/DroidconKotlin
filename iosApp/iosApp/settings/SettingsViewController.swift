//
//  SettingsViewController.swift
//  iosApp
//
//  Created by Kevin Schildhorn on 4/19/19.
//  Copyright © 2019 Kevin Galligan. All rights reserved.
//

import UIKit
import lib
import SwiftUI

class SettingsViewController: MaterialAppBarUIViewController {

    // MARK: Properties
    @IBOutlet weak var tableView: UITableView!
    private var data: [Detail]?
    var viewModel: SettingsViewModel!
    
    // MARK: Lifecycle events
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        viewModel = SettingsViewModel()
        
        tableView.estimatedRowHeight = tableView.rowHeight
        tableView.rowHeight = UITableViewAutomaticDimension
        
        let nib = UINib(nibName: "EventTableViewCell", bundle: nil)
        tableView.register(nib, forCellReuseIdentifier: "eventCell")
        
        tableView.contentInset = UIEdgeInsets.zero
        tableView.separatorStyle = .none
        updateContent()
        
        let name = FeedbackManager.FeedbackDisabledNotificationName
        NotificationCenter.default.addObserver(
            self,
            selector:#selector(updateContent),
            name: Notification.Name(name),
            object: nil
        )
    }
    
    @objc func updateContent(){
        let keys = SettingsKeys()
        let feedbackSwitch = SwitchDetail(
            title: "Enable Feedback",
            image: image(named: "icon_feedback"),
            enabled: bool(for: keys.FEEDBACK_ENABLED),
            listener: { isOn in
                self.setFeedbackSetting(enabled: isOn)
            }
        )
        
        let remindersSwitch = SwitchDetail(
            title: "Enable Reminders",
            image: image(named: "ic_event"),
            enabled: bool(for: keys.REMINDERS_ENABLED),
            listener: { isOn in
                self.setRemindersSetting(enabled: isOn)
            }
        )
        
        let aboutButton = ButtonDetail(
            title: "About",
            image: image(named: "ic_info_outline_white"),
            listener: {
//                self.performSegue(
//                    withIdentifier: "AboutSegue",
//                    sender: nil
//                )
            }
        )

        data = [feedbackSwitch, remindersSwitch, aboutButton]
        tableView.reloadData()
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    // MARK: Helper Functions
    
    func bool(for settingsKey: String) -> Bool {
        return ServiceRegistry().appSettings.getBoolean(
            key: settingsKey,
            defaultValue: true
        )
    }
    
    func setFeedbackSetting(enabled: Bool) {
        viewModel
            .settingsModel
            .setFeedbackSettingEnabled(enabled: enabled)
    }
    
    func setRemindersSetting(enabled: Bool) {
        viewModel
            .settingsModel
            .setFeedbackSettingEnabled(enabled: enabled)
    }
    
    func image(named name: String) -> UIImage {
        return UIImage(named: name) ?? UIImage()
    }

    
    @IBSegueAction func aboutSegueAction(_ coder: NSCoder) -> UIViewController? {
        let aboutViewModel = AboutViewModel()
        return UIHostingController(
            coder: coder,
            rootView: AboutView(viewModel: aboutViewModel)
        )
    }
    
    // MARK: Internal Classes & Enums
    
    enum EntryType{
        case TYPE_BODY
        case TYPE_SWITCH
        case TYPE_BUTTON
    }
    
    public class Detail: NSObject {
        var type:EntryType?
        var title:String?
        var image:UIImage?
        
        init(type:EntryType, title:String, image:UIImage){
            self.type = type
            self.title = title
            self.image = image
        }
    }
    
    private class ButtonDetail: Detail {
        var settingListener: () -> Void
        
        init(title: String, image: UIImage, listener:@escaping () -> Void) {
            self.settingListener = listener
            super.init(type: .TYPE_BUTTON, title: title, image: image)
        }
    }
    
    public class SwitchDetail: Detail {
        var enabled:Bool
        var settingListener: (Bool) -> Void
        
        init(title: String, image: UIImage, enabled: Bool, listener:@escaping (Bool) -> Void) {
            self.settingListener = listener
            self.enabled = enabled
            super.init(type: .TYPE_SWITCH, title: title, image: image)
        }
        
        @objc func onSwitchChanged(sender: UISwitch) {
            enabled = sender.isOn
            settingListener(enabled)
        }
        
    }
}

// MARK: TableView Extensions

extension SettingsViewController: UITableViewDataSource {
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return data?.count ?? 0
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        guard
            let cell = tableView
                .dequeueReusableCell(withIdentifier: "settingCell")
                as? SettingsTableViewCell
            else { return UITableViewCell() }
        
        if let detail = data?[indexPath.row] { cell.loadInfo(detail) }
        cell.selectionStyle = .none
        
        if data?[indexPath.row] is ButtonDetail {
            cell.settingSwitch.isHidden = true
        } else if let detail = data?[indexPath.row] {
            cell.settingSwitch.addTarget(
                detail,
                action: #selector(SwitchDetail.onSwitchChanged(sender:)),
                for: .valueChanged
            )
        }
        
        return cell
    }
}

extension SettingsViewController: UITableViewDelegate {
    func tableView(_ tableView: UITableView, estimatedHeightForRowAt indexPath: IndexPath) -> CGFloat {
        return UITableViewAutomaticDimension
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return UITableViewAutomaticDimension
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: false)

        NotificationsModel().setNotificationsEnabled(enabled: true)

        if let buttonRow = data?[indexPath.row] as? ButtonDetail {
            buttonRow.settingListener()
        }
    }
}
