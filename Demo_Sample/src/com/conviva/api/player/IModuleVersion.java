package com.conviva.api.player;

/**
 * Used by the player interface to set the name and version of a client module towards Conviva SDK.
 */
public interface IModuleVersion {

    /**
     * Return the name of a  client module.
     *
     * @return Module name.  <code>null </code> if not available.
     */
    public String getModuleName();

    /**
     * Return the version of a  client module.
     *
     * @return Module version.  A short string.  <code>null </code> if not available.
     */
    public String getModuleVersion();


    /**
     * Set the name / version of a client module implementation.
     * @param name Module name as a String
     * @param version Module version as a String
     */
    public void setModuleNameAndVersion(String name, String version);


}
