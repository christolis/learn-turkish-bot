package learnenglish.config;

public class Configuration {

    private String token;
    private String channelTranslations;
    private String originalTranslations;
    private String activity;
    private String[] allowedRoles;

    public Configuration() {
    }

    public String getToken() {
        return token;
    }

    public String getChannelTranslationsDir() {
        return channelTranslations;
    }

    public String getOriginalTranslationsDir() {
        return originalTranslations;
    }

    public String getActivity() {
        return activity;
    }

    public String[] getAllowedRoles() {
        return allowedRoles;
    }
}
