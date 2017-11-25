package net.bnetdev.os.javade;

import org.apache.commons.lang3.StringUtils;

import net.bnetdev.os.display.module.DisplayModule;
import net.bnetdev.os.display.service.DisplayService;
import net.bnetdev.os.environment.module.PlatformModule;
import net.bnetdev.os.environment.service.EnvironmentService;
import net.bnetdev.os.factory.terminal.TerminalFactory;
import net.bnetdev.os.filesystem.module.LocalFileModule;
import net.bnetdev.os.filesystem.service.FileSystemService;
import net.bnetdev.os.github.service.GithubService;
import net.bnetdev.os.locale.controller.LocaleController;
import net.bnetdev.os.locale.module.AbstractLocaleModule;
import net.bnetdev.os.locale.service.LocaleService;
import net.bnetdev.os.network.module.ExternalResolverModule;
import net.bnetdev.os.network.service.NetworkService;
import net.bnetdev.project.Project;
import net.bnetdev.project.module.constant.application.ApplicationConstantModule;
import net.bnetdev.project.service.constant.ConstantService;

public class JavaDE extends Project {
	private static JavaDE instance;

	public static void main(String[] args) {
		instance = new JavaDE();
	}

	public JavaDE() {
		JavaDE.getLogger().setDebugMode(false);
		this.initLocale();
		this.greet();
	}

	@Override
	public void greet() {
		ConstantService constants = (ConstantService) registry.getService(ConstantService.NAME);
		ApplicationConstantModule m = (ApplicationConstantModule) constants.getController()
				.getModule(ApplicationConstantModule.NAME);
		String title = m.getConstant("NAME").concat(" v").concat(m.getConstant("VERSION")).concat(" - Developed By - ")
				.concat(m.getConstant("AUTHOR"));

		System.out.println(TerminalFactory.BANNER2);
		JavaDE.getLogger().info(title);
		JavaDE.getLogger().info(m.getConstant("DESCRIPTION"));
		JavaDE.getLogger().info("https://".concat(m.getConstant("URL")));
		System.out.println(TerminalFactory.SEPARATOR);

		EnvironmentService environment = (EnvironmentService) registry.getService(EnvironmentService.NAME);
		PlatformModule platform = (PlatformModule) environment.getController().getModule(PlatformModule.NAME);
		JavaDE.getLogger()
				.info("Running ".concat(StringUtils.capitalize(platform.getPlatform().toString().toLowerCase()))
						.concat(" Kernel ").concat(platform.getVersion()));
	}

	public static JavaDE getInstance() {
		return instance;
	}

	/**
	 * Procedurally Initialize Internals (In no specific order)
	 */

	private void initLocale() {
		LocaleService locale = new LocaleService(registry, null);
		LocaleController lc = (LocaleController) locale.getController();
		AbstractLocaleModule lcm = (AbstractLocaleModule) lc.getModules().get(0);
		this.initConstants(lcm);
	}

	private void initConstants(AbstractLocaleModule alm) {
		ConstantService constant = (ConstantService) registry.getService(ConstantService.NAME);
		ApplicationConstantModule app = (ApplicationConstantModule) constant.getController()
				.getModule(ApplicationConstantModule.NAME);

		app.setAuthor(alm.APPLICATION_AUTHOR());
		app.setName(alm.APPLICATION_NAME());
		app.setDescription(alm.APPLICATION_DESCRIPTION());
		app.setVersion(alm.APPLICATION_VERSION());
		app.setURL(alm.APPLICATION_URL());
		app.setDebug(alm.APPLICATION_DEBUG());

		this.initEnvironment(alm, app);
	}

	private void initEnvironment(AbstractLocaleModule alm, ApplicationConstantModule acm) {
		new EnvironmentService(registry, acm.getConstant("VERSION"));
		this.initNetwork(alm, acm);
	}

	private void initNetwork(AbstractLocaleModule alm, ApplicationConstantModule acm) {
		NetworkService network = new NetworkService(registry, acm.getConstant("VERSION"));
		ExternalResolverModule erm = (ExternalResolverModule) network.getController()
				.getModule(ExternalResolverModule.NAME);

		@SuppressWarnings("unused")
		GithubService github = new GithubService(registry, acm.getConstant("VERSION"));

		boolean isLocal = erm.getIP().equalsIgnoreCase("127.0.0.1");
		if (isLocal) {
			JavaDE.getLogger().debug("External network is unavailable or, not configured.");
		} else {
			JavaDE.getLogger().debug("External network is available.");
		}

		this.initFileSystem(alm, acm, !isLocal);
	}

	private void initFileSystem(AbstractLocaleModule alm, ApplicationConstantModule acm, boolean update) {
		FileSystemService filesystem = new FileSystemService(registry, acm.getConstant("VERSION"));
		LocalFileModule lfm = (LocalFileModule) filesystem.getController().getModule(LocalFileModule.NAME);
		lfm.init();

		if (update) {
			JavaDE.getLogger().debug("Checking for updates..");
		}

		this.initDisplay(alm, acm);
	}

	private void initDisplay(AbstractLocaleModule alm, ApplicationConstantModule acm) {
		DisplayService display = new DisplayService(registry, acm.getConstant("VERSION"));
		DisplayModule dm = (DisplayModule) display.getController().getModule(DisplayModule.NAME);
		if (dm.isAvailable()) {
			JavaDE.getLogger().debug(alm.DISPLAY_IS_AVAILABLE());
			dm.init();
		} else {
			JavaDE.getLogger().debug(alm.DISPLAY_NOT_AVAILABLE());
		}
	}

}
