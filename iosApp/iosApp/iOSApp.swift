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
    }

	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}