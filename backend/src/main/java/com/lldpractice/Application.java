package com.lldpractice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@SpringBootApplication
@EnableAsync
public class Application {

	public static void main(String[] args) {
		loadDotEnv();
		SpringApplication.run(Application.class, args);
	}

	private static void loadDotEnv() {
		File[] possibleFiles = new File[] {
			new File(".env"),
			new File("../.env"),
			new File("backend/.env")
		};
		for (File envFile : possibleFiles) {
			if (envFile.exists() && envFile.isFile()) {
				try {
					List<String> lines = Files.readAllLines(envFile.toPath());
					for (String line : lines) {
						line = line.trim();
						if (line.isEmpty() || line.startsWith("#")) continue;
						int eqIdx = line.indexOf('=');
						if (eqIdx > 0) {
							String key = line.substring(0, eqIdx).trim();
							String value = line.substring(eqIdx + 1).trim();
							if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
								value = value.substring(1, value.length() - 1);
							} else if (value.startsWith("'") && value.endsWith("'") && value.length() >= 2) {
								value = value.substring(1, value.length() - 1);
							}
							if (System.getProperty(key) == null && System.getenv(key) == null) {
								System.setProperty(key, value);
							}
						}
					}
					break;
				} catch (Exception ignored) {}
			}
		}
	}

}
