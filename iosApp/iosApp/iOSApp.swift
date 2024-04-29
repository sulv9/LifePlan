import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        KoinKt.doInitKoin(
            appDeclaration: { _ in
                // Do nothing
            }
        )
        RemindTimeUtilities().initRemindProtocol()
    }

	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
