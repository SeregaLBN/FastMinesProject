using System;
using System.Threading;

namespace fmg.common {

   public static class ThreadLocalRandom {

      private static readonly ThreadLocal<Random> random =
          new ThreadLocal<Random>(() => new Random(Environment.TickCount/* * Environment.CurrentManagedThreadId*/));

      public static Random Current => random.Value;

   }
}
