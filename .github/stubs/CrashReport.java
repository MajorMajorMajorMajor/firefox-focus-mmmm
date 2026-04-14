package mozilla.components.lib.crash.service;

public final class CrashReport {
    public enum Annotation {
        AndroidComponentVersion,
        Android_Board,
        Android_Brand,
        Android_CPU_ABI,
        Android_CPU_ABI2,
        Android_Device,
        Android_Display,
        Android_Fingerprint,
        Android_Hardware,
        Android_Manufacturer,
        Android_Model,
        Android_PackageName,
        Android_ProcessName,
        Android_Version,
        ApplicationBuildID,
        ApplicationServicesVersion,
        Breadcrumbs,
        BuildID,
        CrashID,
        CrashTime,
        CrashType,
        DistributionID,
        GeckoViewVersion,
        GleanVersion,
        InstallTime,
        JavaException,
        JavaStackTrace,
        MinidumpSha256Hash,
        ProcessType,
        ProductID,
        ProductName,
        ReleaseChannel,
        RemoteType,
        StartupTime,
        Vendor,
        Version,
        additional_minidumps,
        useragent_locale
    }

    private CrashReport() {}
}
