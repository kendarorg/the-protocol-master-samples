using Microsoft.AspNetCore;
using System.Text.Json;

var configuration = new ConfigurationBuilder()
        .AddEnvironmentVariables()
        .AddCommandLine(args)
        .AddJsonFile("appsettings.json")
        .Build();
var section = configuration.GetSection("CustomSettings");

WebHost.CreateDefaultBuilder(args)
            .Configure(config =>
            {
                config.UseRouting();
                config.UseEndpoints(endpoints =>
                {
                    endpoints.MapGet("/apiserver", () => section.GetValue<string>("ApiServer"));
                });
                config.UseStaticFiles(/*new StaticFileOptions
                {
                    OnPrepareResponse = ctx =>
                    {
                        ctx.Context.Response.Headers.Append(
                             "API-SERVER", section.GetValue<string>("ApiServer"));
                    }
                }*/);
            })
            .UseWebRoot("wwwroot").Build().Run();