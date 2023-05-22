package eu.coding.commune.startup.validator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.Objects.isNull;

@Getter
@RequiredArgsConstructor
public class PackageProvider {

    private static PackageProvider instance;
    private final ConcurrentLinkedQueue<String> packagesToScan;

    public static PackageProvider getInstance() {
        if (isNull(instance)) {
            instance = new PackageProvider(new ConcurrentLinkedQueue<>());
        }
        return instance;
    }

}
